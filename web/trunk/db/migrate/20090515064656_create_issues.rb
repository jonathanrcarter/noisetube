class CreateIssues < ActiveRecord::Migration
  def self.up
    create_table :issues do |t|
      t.string :title
      t.text :description
      t.integer :city_id
      t.integer :status
      t.integer :creator_id
      t.timestamps
    end
  end

  def self.down
    drop_table :issues
  end
end
